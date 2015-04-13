package verkehrschaosTruck;

import java.util.concurrent.Semaphore;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import verkehrschaos.StreetsHelper;
import verkehrschaos.Truck;
import verkehrschaos.TruckCompany;
import verkehrschaos.TruckCompanyHelper;

public class TruckImpl extends verkehrschaos.TruckPOA {
	private String m_name;
	private TruckCompany m_company;
	private Truck m_this;
	private Semaphore m_running;
	
	private final boolean m_print_coords = true;
	
	TruckImpl(final String name) {
		m_name = name;
		m_company = null;
		m_this = null;
		m_running = new Semaphore(0);
	}

	public void run(final NamingContextExt ncontext, final String company_name) {
		try {
			org.omg.CORBA.Object obj = ncontext.resolve_str(company_name);
			TruckCompany company = TruckCompanyHelper.narrow(obj);
			company.addTruck(m_this);
			m_running.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NotFound e) {
			e.printStackTrace();
		} catch (CannotProceed e) {
			e.printStackTrace();
		} catch (InvalidName e) {
			e.printStackTrace();
		}
	}
	
	public void setTruck(final Truck truck) {
		m_this = truck;
	}
	
	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public TruckCompany getCompany() {
		return m_company;
	}

	@Override
	public void setCompany(TruckCompany company) {
		m_company = company;
	}

	@Override
	public void setCoordinate(double x, double y) {
		if (m_print_coords) {
			System.out.println("[Coords X: " + x + " Y: " + y + "]");
		}
	}

	@Override
	public void putOutOfService() {
		m_company.removeTruck(m_this);
		m_running.release();
	}
}
