package com.nesscomputing.service.activemq.plugins;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SelftestServlet extends HttpServlet
{
    private static final long serialVersionUID = 1;

    public SelftestServlet() { }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setStatus(200);
    }
}
