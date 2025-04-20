package pl.bator.lso_list_generator.model;

import org.jetbrains.annotations.Contract;

public enum SundayMass {
    R, S, P;

    @Contract(pure = true)
    public static SundayMass getNext(SundayMass sundayMass) {
        return switch (sundayMass) {
            case R -> S;
            case S -> P;
            case P -> R;
        };
    }
}
