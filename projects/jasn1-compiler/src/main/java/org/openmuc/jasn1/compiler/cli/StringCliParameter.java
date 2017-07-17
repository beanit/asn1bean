package org.openmuc.jasn1.compiler.cli;

public class StringCliParameter extends ValueCliParameter {

    String value;

    StringCliParameter(CliParameterBuilder builder, String parameterName, String defaultValue) {
        super(builder, parameterName);
        value = defaultValue;
    }

    StringCliParameter(CliParameterBuilder builder, String parameterName) {
        super(builder, parameterName);
    }

    public String getValue() {
        return value;
    }

    @Override
    int parse(String[] args, int i) throws CliParseException {
        selected = true;

        if (args.length < (i + 2)) {
            throw new CliParseException("Parameter " + name + " has no value.");
        }
        value = args[i + 1];

        return 2;
    }

    @Override
    public void appendDescription(StringBuilder sb) {
        super.appendDescription(sb);
        if (value != null) {
            sb.append(" Default is \"").append(value).append("\".");
        }
    }
}
