/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors.support;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;

/**
 * <p>LoopPlugin class.</p>
 *
 * @author david
 * @version $Id: $
 */
public class LoopPlugin {

    private static final String m_protocolName = "LOOP";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    /**
     * <p>getProtocolName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProtocolName() {
        return m_protocolName;
    }

    public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, null);
    }

    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        
        if (qualifiers == null) {
            return false;
        }
        
        String ipMatch = getIpMatch(qualifiers);
        if (IPLike.matches(InetAddressUtils.str(address), ipMatch)) {
            return isSupported(qualifiers);
        } else {
            return false;
        }
        
    }

    private boolean isSupported(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "is-supported", "false").equalsIgnoreCase("true");
    }

    private String getIpMatch(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "ip-match", "*.*.*.*");
    }

}
