package biz.softtechnics.qodeme.utils;

/**
 * Created by Alex on 11/15/13.
 */
public enum Fonts {
    ROBOTO_BOLD("fonts/RobotoBold.ttf"),
    ROBOTO_REGULAR("fonts/RobotoRegular.ttf"),
    ROBOTO_ITALIC("fonts/RobotoItalic.ttf"),
    ROBOTO_BOLD_ITALIC("fonts/RobotoBoldItalic.ttf");

//    CALIBRI_REGULAR("fonts/CALIBRI.TTF"),
//    CALIBRI_BOLD("fonts/CALIBRIB.TTF"),
//    CALIBRI_ITALIC("fonts/CALIBRII.TTF"),
//    CALIBRI_BOLD_ITALIC("fonts/CALIBRIZ.TTF");

    private final String font;

    Fonts(String font) {
        this.font = font;

    }

    @Override
    public String toString() {
        return font;
    }
}
