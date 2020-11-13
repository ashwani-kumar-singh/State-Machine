package com.state.machine.statemachine.service;

import com.state.machine.statemachine.constant.PaymentEvent;
import com.state.machine.statemachine.constant.PaymentState;
import com.state.machine.statemachine.model.Payment;
import org.springframework.statemachine.StateMachine;


public interface PaymentService {

    Payment newPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declinePayment(Long paymentId);



}
