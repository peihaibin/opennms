//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 21: Rework to use FtpResponse. - dj@opennms.org
// 2008 Jan 18: Fix multi-line response handling; bug #1875.  Fix from
//              Victor Jerlin <victor.jerlin@involve.com.mt> - dj@opennms.org
// 2004 May 05: Switch from SocketChannel to Socket with connection timeout.
// 2003 Jul 21: Explicitly closed socket.
// 2003 Jul 18: Enabled retries for monitors.
// 2003 Jun 11: Added a "catch" for RRD update errors. Bug #748.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response times to certain monitors.
// 2002 Nov 14: Used non-blocking I/O socket channel classes.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.Map;

import org.apache.log4j.Level;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.capsd.plugins.FtpResponse;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the FTP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Distributable
final public class FtpMonitor extends AbstractServiceMonitor {

    /**
     * Default FTP port.
     */
    private static final int DEFAULT_PORT = 21;

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on
                                                        // read()

    /**
     * Specific error message generated by some FTP servers when a QUIT is
     * issued by a client when the client has not successfully logged in.
     * 
     * Deprecated - now just check for the return code
     */
    private static final String FTP_ERROR_530_TEXT = "User not logged in. Please login with USER and PASS first";
    private static final String FTP_ERROR_530_TEXT2 = "Not logged in.";
    
    /**
     * Specific error message generated by some FTP servers when a QUIT is
     * issued by a client when the client has not successfully logged in.
     * 
     * Deprecated - now just check for the return code
     */
    private static final String FTP_ERROR_425_TEXT = "425 Session is disconnected.";

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for FTP service availability.
     *
     * During the poll an attempt is made to connect on the specified port (by
     * default TCP port 21). If the connection request is successful, the banner
     * line generated by the interface is parsed and if the extracted return
     * code indicates that we are talking to an FTP server we continue. Next, an
     * FTP 'QUIT' command is sent. Provided that the interface's response is
     * valid we set the service status to SERVICE_AVAILABLE and return.
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        // Check the interface type
        if (iface.getType() != NetworkInterface.TYPE_INET) {
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");
        }

        // Get the parameters
        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);
        String userid = ParameterMap.getKeyedString(parameters, "userid", null);
        String password = ParameterMap.getKeyedString(parameters, "password", null);

        // Extract the address
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        PollStatus serviceStatus = PollStatus.unavailable();
        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {

            if (log().isDebugEnabled()) {
                log().debug("FtpMonitor.poll: Polling interface: " + InetAddressUtils.str(ipv4Addr) + tracker);
            }

            Socket socket = null;
            try {
                // create a connected socket
                tracker.startAttempt();

                socket = new Socket();
                socket.connect(new InetSocketAddress(ipv4Addr, port), tracker.getConnectionTimeout());
                socket.setSoTimeout(tracker.getSoTimeout());
                log().debug("FtpMonitor: connected to host: " + ipv4Addr + " on port: " + port);

                // We're connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.unresponsive();

                BufferedReader lineRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                FtpResponse bannerResponse = FtpResponse.readResponse(lineRdr);

                if (bannerResponse.isSuccess()) {
                    // Attempt to login if userid and password available
                    boolean loggedInSuccessfully = false;
                    log().debug("FtpMonitor: Banner response successful.");
                    if (userid == null || userid.length() == 0 || password == null || password.length() == 0) {
                        loggedInSuccessfully = true;
                    } else {
                        FtpResponse.sendCommand(socket, "USER " + userid);

                        FtpResponse userResponse = FtpResponse.readResponse(lineRdr);

                        if (userResponse.isSuccess() || userResponse.isIntermediate()) {
                            log().debug("FtpMonitor: User response successful.");
                            FtpResponse.sendCommand(socket, "PASS " + password);
                            
                            FtpResponse passResponse = FtpResponse.readResponse(lineRdr);
                            if (passResponse.isSuccess()) {
                                if (log().isDebugEnabled()) {
                                    log().debug("FtpMonitor.poll: Login successful, parsed return code: " + passResponse.getCode());
                                }
                                loggedInSuccessfully = true;
                            } else {
                                if (log().isDebugEnabled()) {
                                    log().debug("FtpMonitor.poll: Login failed, parsed return code: " + passResponse.getCode() + ", full response: " + passResponse.toString());
                                }
                                loggedInSuccessfully = false;
                            }
                        }
                    }

                    // Store the response time before we try to quit
                    double responseTime = tracker.elapsedTimeInMillis();

                    if (loggedInSuccessfully) {
                        FtpResponse.sendCommand(socket, "QUIT");

                        FtpResponse quitResponse = FtpResponse.readResponse(lineRdr);

                        /*
                         * Special Cases for success:
                         * 
                         * Also want to accept the following
                         * ERROR message generated by some FTP servers
                         * following a QUIT command without a previous
                         * successful login:
                         *
                         * "530 QUIT : User not logged in. Please login with
                         * USER and PASS first."
                         * 
                         * Also want to accept the following ERROR
                         * message generated by some FTP servers following a
                         * QUIT command without a previously successful login:
                         *
                         * "425 Session is disconnected."
                         */
                        if (quitResponse.isSuccess()
                                || (quitResponse.getCode() == 530 ) || (quitResponse.getCode() == 425 )) {
                            serviceStatus = PollStatus.available(responseTime);
                        }
                    }
                }

                /*
                 * If we get this far and the status has not been set
                 * to available, then something didn't verify during
                 * the banner checking or login/QUIT command process.
                 */
                if (!serviceStatus.isAvailable()) {
                    serviceStatus = PollStatus.unavailable();
                }
            } catch (NumberFormatException e) {
            	serviceStatus = logDown(Level.DEBUG, "NumberFormatException while polling address: " + ipv4Addr, e);
            } catch (NoRouteToHostException e) {
            	serviceStatus = logDown(Level.WARN, "No route to host exception for address: " + ipv4Addr, e);
            } catch (InterruptedIOException e) {
            	serviceStatus = logDown(Level.DEBUG, "did not connect to host with " + tracker);
            } catch (ConnectException e) {
            	serviceStatus = logDown(Level.DEBUG, "Connection exception for address: " + ipv4Addr, e);
            } catch (IOException e) {
            	serviceStatus = logDown(Level.DEBUG, "IOException while polling address: " + ipv4Addr, e);
            } finally {
                try {
                    // Close the socket
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    log().debug("FtpMonitor.poll: Error closing socket: " + e, e);
                }
            }
        }

        return serviceStatus;
    }
}
