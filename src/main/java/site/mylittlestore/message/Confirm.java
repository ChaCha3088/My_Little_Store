package site.mylittlestore.message;

import lombok.Getter;

@Getter
public class Confirm {

    String message = "";
    String confirmHref = "";
    String cancelHref = "";

    public Confirm(String message, String confirmHref, String cancelHref) {
        this.message = message;
        this.confirmHref = confirmHref;
        this.cancelHref = cancelHref;
    }
}