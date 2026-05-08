package com.backend.domain.payment.dto.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.appointment.entity.Appointment;
import com.backend.domain.order.entity.Order;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.entity.PaymentMethod;
import com.backend.domain.payment.entity.PaymentType;
import com.backend.domain.prescription.entity.Prescription;
import com.backend.domain.user.entity.User;

@Component
public class PaymentMapper {

    public Payment toEntityFromAppointments(User user, List<Appointment> appointments, PaymentMethod paymentMethod,
                                              String accountHolder,
                                              String cashReceiptType, String cashReceiptNumber) {
        BigDecimal totalAmount = appointments.stream()
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Payment.builder()
                .user(user)
                .paymentType(PaymentType.APPOINTMENT)
                .appointments(appointments)
                .amount(totalAmount)
                .paymentMethod(paymentMethod)
                .accountHolder(accountHolder)
                .cashReceiptType(cashReceiptType)
                .cashReceiptNumber(cashReceiptNumber)
                .build();
    }

    public Payment toEntityFromOrder(User user, Order order, PaymentMethod paymentMethod) {
        return Payment.builder()
                .user(user)
                .paymentType(PaymentType.ORDER)
                .order(order)
                .amount(order.getGrandTotal())
                .paymentMethod(paymentMethod)
                .build();
    }

    public Payment toEntityFromPrescription(User user, Prescription prescription, PaymentMethod paymentMethod,
                                               String accountHolder,
                                               String cashReceiptType, String cashReceiptNumber) {
        return Payment.builder()
                .user(user)
                .paymentType(PaymentType.PRESCRIPTION)
                .prescription(prescription)
                .amount(prescription.getPrice())
                .paymentMethod(paymentMethod)
                .accountHolder(accountHolder)
                .cashReceiptType(cashReceiptType)
                .cashReceiptNumber(cashReceiptNumber)
                .build();
    }
}
