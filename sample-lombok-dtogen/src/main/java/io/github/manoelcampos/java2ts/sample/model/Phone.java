package io.github.manoelcampos.java2ts.sample.model;

/**
 * A phone number, used as an embedded value.
 * @param type the phone type, such as mobile or home
 * @param number the phone number
 */
public record Phone(String type, String number) {
}
