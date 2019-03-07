package com.beanit.jasn1.compiler.cli;

public interface ActionListener {

    public void actionCalled(String actionKey) throws ActionException;

    public void quit();

}
