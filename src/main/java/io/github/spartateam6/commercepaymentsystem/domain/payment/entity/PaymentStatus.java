package io.github.spartateam6.commercepaymentsystem.domain.payment.entity;

public enum PaymentStatus {
    PENDING {
        @Override
        public boolean canTransitTo(PaymentStatus status) {
            return status == PAID || status == FAILED;
        }
    },
    PAID {
        @Override
        public boolean canTransitTo(PaymentStatus status) {
            return status == REFUND;
        }
    },
    FAILED {
        @Override
        public boolean canTransitTo(PaymentStatus status) {
            return false;
        }
    },
    REFUND {
        @Override
        public boolean canTransitTo(PaymentStatus status) {
            return false;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus status);
}
