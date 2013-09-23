package kg.apc.perfmon.metrics;

import java.util.Hashtable;

import kg.apc.perfmon.PerfMonMetricGetter;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.hyperic.sigar.SigarProxy;
/**
 *
 * @author undera
 */
public abstract class AbstractPerfMonMetric {

    private static final Logger log = LoggingManager.getLoggerForClass();
    protected static final String PARAMS_DELIMITER = PerfMonMetricGetter.DVOETOCHIE;
    protected final SigarProxy sigarProxy;
    private final static Hashtable unitDividingFactors = new Hashtable();

    public AbstractPerfMonMetric(SigarProxy aSigar) {
        sigarProxy = aSigar;
        unitDividingFactors.put("b", new Integer(1));
        unitDividingFactors.put("kb", new Integer(1024));
        unitDividingFactors.put("mb", new Integer(1024*1024));
    }
    
    protected int getUnitDividingFactor(String unit) {
        if(!unitDividingFactors.containsKey(unit)) {
            return 1;
        } else {
            return ((Integer)unitDividingFactors.get(unit)).intValue();
        }
    }

    abstract public void getValue(StringBuffer res) throws Exception;

    public static AbstractPerfMonMetric createMetric(String metricType, String metricParamsStr, SigarProxy sigarProxy) {
        log.debug("Creating metric: " + metricType + " with params: " + metricParamsStr);
        AbstractPerfMonMetric metric;
        if (metricType.indexOf(' ') > 0) {
            metricType = metricType.substring(0, metricType.indexOf(' '));
        }

        MetricParamsSigar metricParams = MetricParamsSigar.createFromString(metricParamsStr, sigarProxy);
        PerfMonMetricsService service = PerfMonMetricsService.getInstance();
        
        try {
        	metric = service.getMetric(metricType, metricParams, sigarProxy);
        } catch (IllegalArgumentException ex) {
            log.error(ex.toString());
            log.error("Invalid parameters specified for metric " + metricType + ": " + metricParams);
            metric = new InvalidPerfMonMetric();
        } catch (RuntimeException ex) {
            log.error("Invalid metric specified: " + metricType, ex);
            metric = new InvalidPerfMonMetric();
        }

        log.debug("Have metric object: " + metric.toString());
        return metric;
    }
}
