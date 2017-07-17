package org.openmuc.jasn1.compiler.cli;

public class IntCliParameter extends ValueCliParameter {

    Integer value;

    IntCliParameter(CliParameterBuilder builder, String parameterName, int defaultValue) {
        super(builder, parameterName);
        value = defaultValue;
    }

    IntCliParameter(CliParameterBuilder builder, String parameterName) {
        super(builder, parameterName);
    }

    public int getValue() {
        return value;
    }

    @Override
    int parse(String[] args, int i) throws CliParseException {
        selected = true;

        if (args.length < (i + 2)) {
            throw new CliParseException("Parameter " + name + " has no value.");
        }

        try {
            value = Integer.decode(args[i + 1]);
        } catch (Exception e) {
            throw new CliParseException("Parameter value " + args[i + 1] + " cannot be converted to int.");
        }
        return 2;
    }

    @Override
    void appendDescription(StringBuilder sb) {
        super.appendDescription(sb);
        if (value != null) {
            sb.append(" Default is ").append(value).append(".");
        }
    }
}
