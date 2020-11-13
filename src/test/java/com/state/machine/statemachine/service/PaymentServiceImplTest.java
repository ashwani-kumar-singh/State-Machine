package com.state.machine.statemachine.service;

import com.state.machine.statemachine.constant.PaymentEvent;
import com.state.machine.statemachine.constant.PaymentState;
import com.state.machine.statemachine.model.Payment;
import com.state.machine.statemachine.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setAmount(new BigDecimal("12.99"));
    }

    @Test
    void newPayment() {
    }

    @Test
    @Transactional
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        System.out.println("Before PreAuth: " + savedPayment.getState());
        StateMachine<PaymentState, PaymentEvent> sm =  paymentService.preAuth(payment.getId());
        Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());
        System.out.println("After PreAuth: " + sm.getState().getId());
        System.out.println("After PreAuth: " + preAuthPayment.getState());
    }

    @Test
    @Transactional
    void authorizePayment() {
        Payment savedPayment = paymentService.newPayment(payment);
        System.out.println("Before PreAuth: " + savedPayment.getState());
        StateMachine<PaymentState, PaymentEvent> preAuthSm =  paymentService.preAuth(payment.getId());
        if(preAuthSm.getState().getId() == PaymentState.PRE_AUTH){
            System.out.println("Pre Authorization Successful");
            StateMachine<PaymentState, PaymentEvent> authSM = paymentService.authorizePayment(payment.getId());
            System.out.println("Result of Auth: " + authSM.getState().getId());
        }else {
            System.out.println("Pre Authorization got failed!");
        }
    }

    @Test
    void declinePayment() {
    }
}