package org.openmuc.jasn1.compiler.cli;

public interface ActionListener {

    public void actionCalled(String actionKey) throws ActionException;

    public void quit();

}
