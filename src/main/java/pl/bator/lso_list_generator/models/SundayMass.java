package pl.bator.lso_list_generator.models;

public enum SundayMass {
    R, S, P;

    public static SundayMass getNext(SundayMass sundayMass) {
        return switch (sundayMass) {
            case R -> S;
            case S -> P;
            case P -> R;
        };
    }
}
