/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.simple;


/**
 * Detects the presence of the Citrix service on an IP address using
 * a line oriented protocol.
 *
 * @author <a href="mailto:thedesloge@opennms.org">Donald Desloge</a>
 * @version $Id: $
 */


public class CitrixDetector extends AsyncLineOrientedDetectorMinaImpl {
    
    
    private static final String DEFAULT_SERVICE_NAME = "CITRIX";
    private static final int DEFAULT_PORT = 1494;

    /**
     * Default constructor
     */
    public CitrixDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public CitrixDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(startsWith("ICA"));
    }


}
