package com.state.machine.statemachine.service;

import com.state.machine.statemachine.constant.PaymentEvent;
import com.state.machine.statemachine.constant.PaymentState;
import com.state.machine.statemachine.model.Payment;
import com.state.machine.statemachine.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment_id";

    @Autowired
    private PaymentStateChangeInterceptor stateChangeInterceptor;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTH_APPROVED);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declinePayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
        return sm;
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){
        Payment payment = paymentRepository.getOne(paymentId);
        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(Long.toString(paymentId));
        sm.stop();
        sm.getStateMachineAccessor()
                .doWithAllRegions( sma -> {
                            sma.addStateMachineInterceptor(stateChangeInterceptor);
                            sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                        });
        sm.start();
        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event){
        Message msg = MessageBuilder.withPayload(event).setHeader(PAYMENT_ID_HEADER, paymentId).build();
        sm.sendEvent(msg);
    }
}
