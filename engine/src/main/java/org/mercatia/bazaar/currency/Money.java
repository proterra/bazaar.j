package org.mercatia.bazaar.currency;

import org.mercatia.bazaar.utils.Range;
import java.math.*;

public class Money extends Range.RangeType<Money> {

    private Currency currency;

    private long unit;
    private long fractional;

    protected Money() {

    }

    public Currency getCurrency() {
        return currency;
    }

    public long getUnit() {
        return unit;
    }

    public long getFractional() {
        return fractional;
    }

    protected Money(Currency currency, long unit, long fractional) {
        this.currency = currency;
        this.unit = unit;
        this.fractional = fractional;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Money from(Currency c, double f) {

        var str = String.format("%01.2f", f);
        var decimalIndex = str.indexOf(".");
        var unit = Long.valueOf(str.substring(0, decimalIndex));
        var y = Long.valueOf(str.substring(decimalIndex + 1));

        return builder().currency(c).unit(unit).fractional(y).build();
    }

    public static class Builder {
        long unit;
        long fractional;
        Currency currency;

        private Builder() {

        }

        public Builder currency(Currency c) {
            currency = c;
            return this;
        }

        public Builder unit(long u) {
            unit = u;
            return this;
        }

        public Builder fractional(long u) {
            fractional = u;
            return this;
        }

        public Money build() {
            return new Money(currency, unit, fractional);
        }
    }

    protected Money addUnit(long u) {
        return builder().currency(this.currency).unit(this.unit + u).fractional(this.fractional).build();
    }

    protected Money subtractUnit(long u) {
        return builder().currency(this.currency).unit(this.unit - u).fractional(this.fractional).build();
    }

    protected Money subtractFractional(long u) {
        var x = ((this.unit * 100) + this.fractional) - u;
        var newFractional = x % 100;
        var newUnit = (x - newFractional) / 100;

        return builder().currency(this.currency).unit(newUnit).fractional(newFractional).build();
    }

    protected Money addFractional(long u) {
        var x = ((this.unit * 100) + this.fractional) + u;
        var newFractional = x % 100;
        var newUnit = (x - newFractional) / 100;

        return builder().currency(this.currency).unit(newUnit).fractional(newFractional).build();
    }

    @Override
    public Money add(Money x) {
        if (x == null) {
            return this;
        }
        return addFractional(x.getFractional()).addUnit(x.getUnit());
    }

    @Override
    public Money subtract(Money x) {
        if (x == null) {
            return this;
        }
        return subtractUnit(x.getUnit()).subtractFractional(x.getFractional());
    }

    @Override
    public Money multiply(Money other) {
        return this.multiply(other.as());
    }

    @Override
    public Money multiply(double x) {
        return Money.from(currency, as() * x);
    }

    @Override
    public double as() {
        var fbd = new BigDecimal(this.fractional).movePointLeft(2);
        var ubd = new BigDecimal(this.unit);

        var r = ubd.add(fbd);
        r = r.setScale(2, RoundingMode.HALF_DOWN);
        return r.doubleValue();
    }

    public String toString() {
        var sign = (unit < 0 || fractional < 0) ? "-" : "";

        return String.format("%s%d.%02d", sign, Math.abs(unit), Math.abs(fractional));
    }

    @Override
    public Money toNew(double f) {
        return Money.from(this.getCurrency(), f);
    }

    @Override
    public boolean zeroOrLess() {
        return as() <= 0.0f;
    }

    public static Money NONE() {
        return builder().currency(Currency.DEFAULT).unit(0).fractional(0).build();
    }

    @Override
    public boolean zeroOrGreater() {
        return as() >= 0.0f;
    }

    @Override
    public boolean greater(Money other) {
        return as() > other.as();
    }

    @Override
    public boolean less(Money other) {
        return as() < other.as();
    }

}