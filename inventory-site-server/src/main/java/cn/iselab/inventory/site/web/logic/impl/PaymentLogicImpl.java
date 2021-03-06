package cn.iselab.inventory.site.web.logic.impl;

import cn.iselab.inventory.site.common.constanst.OrderStatusConstants;
import cn.iselab.inventory.site.model.Account;
import cn.iselab.inventory.site.model.Custom;
import cn.iselab.inventory.site.model.Payment;
import cn.iselab.inventory.site.model.PaymentEntry;
import cn.iselab.inventory.site.service.AccountService;
import cn.iselab.inventory.site.service.CustomService;
import cn.iselab.inventory.site.service.PaymentEntryService;
import cn.iselab.inventory.site.service.PaymentService;
import cn.iselab.inventory.site.web.data.PaymentVO;
import cn.iselab.inventory.site.web.data.wrapper.PaymentVOWrapper;
import cn.iselab.inventory.site.web.exception.HttpBadRequestException;
import cn.iselab.inventory.site.web.logic.PaymentLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author ROKG
 * @Description
 * @Date: Created in 下午9:59 2017/12/6
 * @Modified By:
 */
@Service
public class PaymentLogicImpl implements PaymentLogic{

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentEntryService paymentEntryService;

    @Autowired
    PaymentVOWrapper paymentVOWrapper;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomService customService;

    @Override
    public Page<PaymentVO> getPayments(String keyword, Pageable pageable){
        Page<Payment> payments=paymentService.getPayments(keyword,pageable);
        return payments.map(new Converter<Payment, PaymentVO>() {
            @Override
            public PaymentVO convert(Payment payment) {
                return paymentVOWrapper.wrap(payment);
            }
        });
    }

    @Override
    public PaymentVO getPayment(String number){
        Payment payment=paymentService.getPaymentByNum(number);
        if (payment == null) {
            throw new HttpBadRequestException("payment not exists");
        }
        PaymentVO vo= paymentVOWrapper.wrap(payment);
        vo.setEntries(paymentEntryService.getPaymentEntries(payment.getId()));
        return vo;
    }

    @Override
    public String createPayment(PaymentVO vo){
        Payment payment=paymentVOWrapper.unwrap(vo);
        payment=paymentService.createPayment(payment);
        List<PaymentEntry> entries=vo.getEntries();
        for (PaymentEntry entry:entries){
            entry.setPayment(payment.getId());
            paymentEntryService.createPaymentEntry(entry);
        }
        return payment.getNumber();
    }

    @Override
    public void checkPayment(Payment payment){
        Account account=accountService.getAccount(payment.getAccount());
        account.setBalance(account.getBalance()-payment.getTotal());
        accountService.updateAccount(account);
        Custom custom=customService.getCustom(payment.getCustom());
        custom.setReceive(custom.getReceive()+payment.getTotal());
        customService.updateCustom2(custom);
    }

    @Override
    public void updatePayment(PaymentVO vo){
        Payment payment=paymentService.getPaymentByNum(vo.getNumber());
        if (payment == null) {
            throw new HttpBadRequestException("payment not exists");
        }
        updateInfo(payment,vo);
        paymentService.updatePayment(payment);
        if(vo.getStatus()== OrderStatusConstants.APPROVED){
            checkPayment(payment);
        }
    }

    @Override
    public void deletePayment(String number){
        Payment payment=paymentService.getPaymentByNum(number);
        if (payment == null) {
            throw new HttpBadRequestException("payment not exists");
        }
        paymentService.deletePayment(payment);
    }

    private void updateInfo(Payment payment,PaymentVO vo){
        payment.setTotal(vo.getTotal());
        payment.setStatus(vo.getStatus());
        if (vo.getAccount()!=null) {
            payment.setAccount(vo.getAccountId());
        }
    }

}
