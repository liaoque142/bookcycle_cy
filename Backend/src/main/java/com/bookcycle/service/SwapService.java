package com.bookcycle.service;

import com.bookcycle.common.PageResult;
import com.bookcycle.dto.swap.AcceptSwapRequest;
import com.bookcycle.dto.swap.CancelSwapRequest;
import com.bookcycle.dto.swap.CreateSwapRequest;
import com.bookcycle.entity.Book;
import com.bookcycle.entity.Swap;
import com.bookcycle.entity.SysUser;
import com.bookcycle.exception.BusinessException;
import com.bookcycle.mapper.AddressMapper;
import com.bookcycle.mapper.BookMapper;
import com.bookcycle.mapper.FriendMapper;
import com.bookcycle.mapper.SwapMapper;
import com.bookcycle.mapper.UserMapper;
import com.bookcycle.vo.SwapVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 交换服务：以书换书（买家以书 B 换卖家书 A），状态机：
 * 1 待对方同意 → 接收方 同意→2 / 拒绝→6；发起人取消→5
 * 2 已同意待发货 → 双方各自发货（书 已预订→已售出），双方都发→3；未发货方可取消→5
 * 3 交换中 → 双方各自确认收货（需对方已发），双方都确认→4
 * 4 已完成（双方信誉 TRADE_SUCCESS +2）
 * 5 已取消 / 6 已拒绝
 */
@Service
@RequiredArgsConstructor
public class SwapService {

    private static final int BOOK_ON_SALE = 1;
    private static final int BOOK_RESERVED = 2;
    private static final int BOOK_SOLD = 3;
    private static final int TRADE_TYPE_SWAP = 3;

    private static final int SWAP_PENDING = 1;
    private static final int SWAP_ACCEPTED = 2;
    private static final int SWAP_SHIPPING = 3;
    private static final int SWAP_COMPLETED = 4;
    private static final int SWAP_CANCELLED = 5;
    private static final int SWAP_REJECTED = 6;

    private final SwapMapper swapMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final FriendMapper friendMapper;
    private final CreditService creditService;
    private final NotificationService notificationService;

    /** 发起交换：用我的在售书 B 换卖家在售交换书 A；双书 CAS 在售→已预订 防并发 */
    @Transactional
    public SwapVO create(CreateSwapRequest req, Long userId) {
        SysUser initiator = userMapper.selectById(userId);
        if (initiator == null) {
            throw new BusinessException("用户不存在");
        }
        int credit = initiator.getCredit() != null ? initiator.getCredit() : 0;
        if (credit < 40) {
            throw new BusinessException("信誉分低于 40，交易功能已冻结");
        }
        if (req.getOfferBookId().equals(req.getBookId())) {
            throw new BusinessException("不能用同一本书进行交换");
        }
        Book target = bookMapper.selectEntityById(req.getBookId());
        if (target == null || target.getStatus() != BOOK_ON_SALE) {
            throw new BusinessException("书籍不存在或已被预订");
        }
        if (target.getTradeType() != TRADE_TYPE_SWAP) {
            throw new BusinessException("该书籍不支持交换");
        }
        if (target.getUserId().equals(userId)) {
            throw new BusinessException("不能换自己发布的书籍");
        }
        Book offer = bookMapper.selectEntityById(req.getOfferBookId());
        if (offer == null || offer.getStatus() != BOOK_ON_SALE) {
            throw new BusinessException("用于交换的书籍不存在或已被预订");
        }
        if (!offer.getUserId().equals(userId)) {
            throw new BusinessException("只能用自己的书籍进行交换");
        }
        if (friendMapper.countBlackBetween(userId, target.getUserId()) > 0) {
            throw new BusinessException("双方存在拉黑关系，无法交易");
        }
        Long addressId = null;
        if (req.getShipType() == 1) { // 邮寄
            if (req.getAddressId() == null) {
                throw new BusinessException("邮寄交换请选择收货地址");
            }
            var address = addressMapper.selectById(req.getAddressId());
            if (address == null || !address.getUserId().equals(userId)) {
                throw new BusinessException("收货地址不存在或无权使用");
            }
            addressId = req.getAddressId();
        }
        // 双书 CAS 在售→已预订；任一步失败即抛异常，事务回滚（含已成功的 CAS）
        if (bookMapper.updateStatusIfEquals(target.getId(), BOOK_ON_SALE, BOOK_RESERVED) == 0) {
            throw new BusinessException("手慢了，该书已被预订");
        }
        if (bookMapper.updateStatusIfEquals(offer.getId(), BOOK_ON_SALE, BOOK_RESERVED) == 0) {
            throw new BusinessException("您用于交换的书籍已被预订");
        }
        Swap swap = new Swap();
        swap.setSwapNo(generateSwapNo());
        swap.setInitiatorId(userId);
        swap.setReceiverId(target.getUserId());
        swap.setBookId(target.getId());
        swap.setOfferBookId(offer.getId());
        swap.setMessage(StringUtils.hasText(req.getMessage()) ? req.getMessage().trim() : null);
        swap.setStatus(SWAP_PENDING);
        swap.setInitiatorShipType(req.getShipType());
        swap.setInitiatorAddressId(addressId);
        swapMapper.insert(swap);
        notificationService.notify(target.getUserId(), NotificationService.TYPE_TRADE,
                "书友想用《" + offer.getTitle() + "》交换您的《" + target.getTitle() + "》，请前往处理");
        return detail(swap.getSwapNo(), userId);
    }

    /** 接收方同意：待对方同意 → 已同意待发货，记录接收方收书地址 */
    @Transactional
    public SwapVO accept(String swapNo, AcceptSwapRequest req, Long userId) {
        Swap swap = requireSwap(swapNo);
        requireReceiver(swap, userId);
        Long addressId = null;
        if (req.getShipType() == 1) { // 邮寄
            if (req.getAddressId() == null) {
                throw new BusinessException("邮寄交换请选择收货地址");
            }
            var address = addressMapper.selectById(req.getAddressId());
            if (address == null || !address.getUserId().equals(userId)) {
                throw new BusinessException("收货地址不存在或无权使用");
            }
            addressId = req.getAddressId();
        }
        if (swapMapper.accept(swap.getId(), req.getShipType(), addressId) == 0) {
            throw new BusinessException("该交换已被处理");
        }
        notificationService.notify(swap.getInitiatorId(), NotificationService.TYPE_TRADE,
                "对方已同意交换，交换单 " + swapNo + "，请发货");
        return detail(swapNo, userId);
    }

    /** 接收方拒绝：待对方同意 → 已拒绝，两书释放回在售 */
    @Transactional
    public SwapVO reject(String swapNo, Long userId) {
        Swap swap = requireSwap(swapNo);
        requireReceiver(swap, userId);
        if (swapMapper.updateStatus(swap.getId(), SWAP_PENDING, SWAP_REJECTED) == 0) {
            throw new BusinessException("该交换已被处理");
        }
        releaseBoth(swap);
        notificationService.notify(swap.getInitiatorId(), NotificationService.TYPE_TRADE,
                "对方拒绝了交换，交换单 " + swapNo);
        return detail(swapNo, userId);
    }

    /** 取消：待对方同意/已同意待发货 且 本人未发货 可取消；两书释放回在售（已发货的书保持已售出） */
    @Transactional
    public SwapVO cancel(String swapNo, CancelSwapRequest req, Long userId) {
        Swap swap = requireSwap(swapNo);
        boolean isInitiator = swap.getInitiatorId().equals(userId);
        boolean isReceiver = swap.getReceiverId().equals(userId);
        if (!isInitiator && !isReceiver) {
            throw new BusinessException(403, "无权操作该交换");
        }
        int status = swap.getStatus();
        if (status == SWAP_CANCELLED) {
            throw new BusinessException("该交换已取消");
        }
        if (status == SWAP_COMPLETED) {
            throw new BusinessException("交换已完成，无法取消");
        }
        if (status == SWAP_REJECTED) {
            throw new BusinessException("该交换已拒绝");
        }
        if (status == SWAP_SHIPPING) {
            throw new BusinessException("双方已发货，无法取消");
        }
        if (isInitiator && swap.getInitiatorShippedAt() != null) {
            throw new BusinessException("您已发货，无法取消");
        }
        if (isReceiver && swap.getReceiverShippedAt() != null) {
            throw new BusinessException("您已发货，无法取消");
        }
        if (swapMapper.cancel(swap.getId(), req.getReason()) == 0) {
            throw new BusinessException("当前状态不可取消");
        }
        releaseBoth(swap);
        Long otherId = isInitiator ? swap.getReceiverId() : swap.getInitiatorId();
        notificationService.notify(otherId, NotificationService.TYPE_TRADE,
                "交换单 " + swapNo + " 已取消" + (StringUtils.hasText(req.getReason()) ? "：" + req.getReason() : ""));
        return detail(swapNo, userId);
    }

    /** 发货：本人发自己的书（发起人发书 B / 接收方发书 A），书 已预订→已售出；双方都发后状态 → 交换中 */
    @Transactional
    public SwapVO ship(String swapNo, Long userId) {
        Swap swap = requireSwap(swapNo);
        boolean isInitiator = swap.getInitiatorId().equals(userId);
        boolean isReceiver = swap.getReceiverId().equals(userId);
        if (!isInitiator && !isReceiver) {
            throw new BusinessException(403, "无权操作该交换");
        }
        if (swap.getStatus() != SWAP_ACCEPTED) {
            throw new BusinessException("当前状态不可发货");
        }
        Long bookId;
        if (isInitiator) {
            if (swap.getInitiatorShippedAt() != null) {
                throw new BusinessException("您已发货");
            }
            swapMapper.markInitiatorShipped(swap.getId());
            bookId = swap.getOfferBookId();
        } else {
            if (swap.getReceiverShippedAt() != null) {
                throw new BusinessException("您已发货");
            }
            swapMapper.markReceiverShipped(swap.getId());
            bookId = swap.getBookId();
        }
        bookMapper.updateStatusIfEquals(bookId, BOOK_RESERVED, BOOK_SOLD);
        boolean bothShipped = swapMapper.markBothShipped(swap.getId()) == 1;
        Long otherId = isInitiator ? swap.getReceiverId() : swap.getInitiatorId();
        notificationService.notify(otherId, NotificationService.TYPE_TRADE,
                "对方已发货，请等待收书" + (bothShipped ? "（双方均已发货，请确认收货）" : ""));
        return detail(swapNo, userId);
    }

    /** 确认收货：本人确认收到对方的书（需对方已发货）；双方都确认后 → 已完成，双方信誉 +2 */
    @Transactional
    public SwapVO confirm(String swapNo, Long userId) {
        Swap swap = requireSwap(swapNo);
        boolean isInitiator = swap.getInitiatorId().equals(userId);
        boolean isReceiver = swap.getReceiverId().equals(userId);
        if (!isInitiator && !isReceiver) {
            throw new BusinessException(403, "无权操作该交换");
        }
        if (swap.getStatus() != SWAP_ACCEPTED && swap.getStatus() != SWAP_SHIPPING) {
            throw new BusinessException("当前状态不可确认收货");
        }
        if (isInitiator) {
            if (swap.getReceiverShippedAt() == null) {
                throw new BusinessException("对方尚未发货，无法确认收货");
            }
            if (swap.getInitiatorConfirmedAt() != null) {
                throw new BusinessException("您已确认收货");
            }
            swapMapper.markInitiatorConfirmed(swap.getId());
        } else {
            if (swap.getInitiatorShippedAt() == null) {
                throw new BusinessException("对方尚未发货，无法确认收货");
            }
            if (swap.getReceiverConfirmedAt() != null) {
                throw new BusinessException("您已确认收货");
            }
            swapMapper.markReceiverConfirmed(swap.getId());
        }
        if (swapMapper.markBothConfirmed(swap.getId()) == 1) {
            creditService.apply(swap.getInitiatorId(), "TRADE_SUCCESS", null);
            creditService.apply(swap.getReceiverId(), "TRADE_SUCCESS", null);
            notificationService.notify(swap.getInitiatorId(), NotificationService.TYPE_TRADE,
                    "交换单 " + swapNo + " 已完成，双方书籍交换成功");
            notificationService.notify(swap.getReceiverId(), NotificationService.TYPE_TRADE,
                    "交换单 " + swapNo + " 已完成，双方书籍交换成功");
        }
        return detail(swapNo, userId);
    }

    public PageResult<SwapVO> list(String role, Integer status, int page, int size, Long userId) {
        PageHelper.startPage(page, size);
        List<SwapVO> list = swapMapper.selectPage(role, status, userId);
        return PageResult.of(new PageInfo<>(list));
    }

    public SwapVO detail(String swapNo, Long userId) {
        SwapVO vo = swapMapper.selectVOBySwapNo(swapNo);
        if (vo == null) {
            throw new BusinessException("交换单不存在");
        }
        if (!vo.getInitiatorId().equals(userId) && !vo.getReceiverId().equals(userId)) {
            throw new BusinessException(403, "无权查看该交换单");
        }
        return vo;
    }

    /** 释放两书预订（已售出的书不匹配 status=2 无副作用） */
    private void releaseBoth(Swap swap) {
        bookMapper.updateStatusIfEquals(swap.getBookId(), BOOK_RESERVED, BOOK_ON_SALE);
        bookMapper.updateStatusIfEquals(swap.getOfferBookId(), BOOK_RESERVED, BOOK_ON_SALE);
    }

    private Swap requireSwap(String swapNo) {
        Swap swap = swapMapper.selectBySwapNo(swapNo);
        if (swap == null) {
            throw new BusinessException("交换单不存在");
        }
        return swap;
    }

    private void requireReceiver(Swap swap, Long userId) {
        if (!swap.getReceiverId().equals(userId)) {
            throw new BusinessException(403, "只有接收方可以执行该操作");
        }
    }

    /** 交换单号：SW + 时间戳(17) + 6 位随机，<=32 字符 */
    private String generateSwapNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "SW" + ts + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }
}
