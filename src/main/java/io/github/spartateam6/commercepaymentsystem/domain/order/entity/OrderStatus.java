package io.github.spartateam6.commercepaymentsystem.domain.order.entity;

public enum OrderStatus {

    PAYMENT_PENDING {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == CONFIRMED || target == CANCELLED || target == FAILED;
        }
    },

    CONFIRMED {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == CANCELLED;
        }
    },

    CANCELLED {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return false;
        }
    },

    FAILED {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(OrderStatus target);
}