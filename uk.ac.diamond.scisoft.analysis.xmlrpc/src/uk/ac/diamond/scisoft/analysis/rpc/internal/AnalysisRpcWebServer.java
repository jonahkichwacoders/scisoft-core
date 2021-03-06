/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rpc.internal;

import java.net.InetAddress;

import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.webserver.WebServer;

/**
 * Custom {@link WebServer} that has our own type factory.
 * 
 * @see AnalysisRpcDoubleParser
 * @see AnalysisRpcServerHandlerImpl
 */
public class AnalysisRpcWebServer extends WebServer {

	// This constructor is currently unused, but included for completeness to 
	// provide same construction options as WebServer
	public AnalysisRpcWebServer(int pPort) {
		super(pPort);
	}

	public AnalysisRpcWebServer(int pPort, InetAddress pAddr) {
		super(pPort, pAddr);
	}

	@Override
	protected XmlRpcStreamServer newXmlRpcStreamServer() {
		XmlRpcStreamServer server = super.newXmlRpcStreamServer();
		server.setTypeFactory(new AnalysisRpcTypeFactoryImpl(server));
		return server;
	}

}
