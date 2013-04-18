package fr.labri.harmony.core;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class DaoUtil {

	public static Dao getDao(Class<?> callingClass) {
		BundleContext context = FrameworkUtil.getBundle(callingClass).getBundleContext();
		ServiceReference<Dao> ref = context.getServiceReference(Dao.class);
		return context.getService(ref);
	}

}
