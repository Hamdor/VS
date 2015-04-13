package verkehrschaosTruck;

import java.util.concurrent.Semaphore;

import org.omg.CosNaming.NamingContextExt;

import verkehrschaos.Truck;
import verkehrschaos.TruckCompany;

public class TruckImpl extends verkehrschaos.TruckPOA {
	private String m_name;
	private TruckCompany m_company;
	private Truck m_this;
	private Semaphore m_running;
	
	public void run(final NamingContextExt namecontext, final String company_name) {
		// TODO:
		// get company, register at company
		// block till end => unregister from company...
		try {
			m_running.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setTruck(final Truck truck) {
		m_this = truck;
	}
	
	public void setName(final String name) {
		m_name = name;
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
		System.out.println("[Coords X: " + x + " Y: " + y + "]");
	}

	@Override
	public void putOutOfService() {
		m_company.removeTruck(m_this);
		m_running.release();
	}
}
