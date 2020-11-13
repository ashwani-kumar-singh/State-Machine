package com.state.machine.statemachine.service;

import com.state.machine.statemachine.constant.PaymentEvent;
import com.state.machine.statemachine.constant.PaymentState;
import com.state.machine.statemachine.model.Payment;
import com.state.machine.statemachine.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState,
            PaymentEvent> stateMachine) {
        Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable(
                (Long) message.getHeaders()
                        .getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
                .ifPresent(paymentId -> {
            Payment payment = paymentRepository.getOne(paymentId);
            payment.setState(state.getId());
            paymentRepository.save(payment);
        });
    }
}
