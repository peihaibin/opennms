package org.opennms.netmgt.xml.event;

import javax.xml.bind.annotation.XmlRegistry;

import org.opennms.core.utils.LogUtils;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opennms.xmlns.xsd.event package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opennms.xmlns.xsd.event
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AlarmData }
     * 
     */
    public AlarmData createAlarmData() {
    	LogUtils.debugf(this, "createAlarmData");
        return new AlarmData();
    }

    /**
     * Create an instance of {@link Value }
     * 
     */
    public Value createValue() {
    	LogUtils.debugf(this, "createValue");
        return new Value();
    }

    /**
     * Create an instance of {@link Events }
     * 
     */
    public Events createEvents() {
    	LogUtils.debugf(this, "createEvents");
        return new Events();
    }

    /**
     * Create an instance of {@link Maskelement }
     * 
     */
    public Maskelement createMaskelement() {
    	LogUtils.debugf(this, "createMaskelement");
        return new Maskelement();
    }

    /**
     * Create an instance of {@link Log }
     * 
     */
    public Log createLog() {
    	LogUtils.debugf(this, "createLog");
        return new Log();
    }

    /**
     * Create an instance of {@link Forward }
     * 
     */
    public Forward createForward() {
    	LogUtils.debugf(this, "createForward");
        return new Forward();
    }

    /**
     * Create an instance of {@link Parms }
     * 
     */
    public Parms createParms() {
    	LogUtils.debugf(this, "createParms");
        return new Parms();
    }

    /**
     * Create an instance of {@link Event }
     * 
     */
    public Event createEvent() {
    	LogUtils.debugf(this, "createEvent");
        return new Event();
    }

    /**
     * Create an instance of {@link Header }
     * 
     */
    public Header createHeader() {
    	LogUtils.debugf(this, "createHeader");
        return new Header();
    }

    /**
     * Create an instance of {@link Logmsg }
     * 
     */
    public Logmsg createLogmsg() {
    	LogUtils.debugf(this, "createLogmsg");
        return new Logmsg();
    }

    /**
     * Create an instance of {@link Mask }
     * 
     */
    public Mask createMask() {
    	LogUtils.debugf(this, "createMask");
        return new Mask();
    }

    /**
     * Create an instance of {@link Operaction }
     * 
     */
    public Operaction createOperaction() {
    	LogUtils.debugf(this, "createOperaction");
        return new Operaction();
    }

    /**
     * Create an instance of {@link Autoacknowledge }
     * 
     */
    public Autoacknowledge createAutoacknowledge() {
    	LogUtils.debugf(this, "createAutoacknowledge");
        return new Autoacknowledge();
    }

    /**
     * Create an instance of {@link Parm }
     * 
     */
    public Parm createParm() {
    	LogUtils.debugf(this, "createParm");
        return new Parm();
    }

    /**
     * Create an instance of {@link Snmp }
     * 
     */
    public Snmp createSnmp() {
    	LogUtils.debugf(this, "createSnmp");
        return new Snmp();
    }

    /**
     * Create an instance of {@link Script }
     * 
     */
    public Script createScript() {
    	LogUtils.debugf(this, "createScript");
        return new Script();
    }

    /**
     * Create an instance of {@link EventReceipt }
     * 
     */
    public EventReceipt createEventReceipt() {
    	LogUtils.debugf(this, "createEventReceipt");
        return new EventReceipt();
    }

    /**
     * Create an instance of {@link Correlation }
     * 
     */
    public Correlation createCorrelation() {
    	LogUtils.debugf(this, "createCorrelation");
        return new Correlation();
    }

    /**
     * Create an instance of {@link Autoaction }
     * 
     */
    public Autoaction createAutoaction() {
    	LogUtils.debugf(this, "createAutoaction");
        return new Autoaction();
    }

    /**
     * Create an instance of {@link Tticket }
     * 
     */
    public Tticket createTticket() {
    	LogUtils.debugf(this, "createTticket");
        return new Tticket();
    }

}
