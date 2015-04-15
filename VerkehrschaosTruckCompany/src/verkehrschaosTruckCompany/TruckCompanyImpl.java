package verkehrschaosTruckCompany;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import verkehrschaos.ELocationInUse;
import verkehrschaos.ELocationNotFound;
import verkehrschaos.Streets;
import verkehrschaos.StreetsHelper;
import verkehrschaos.TTruckListHolder;
import verkehrschaos.Truck;
import verkehrschaos.TruckCompany;

public class TruckCompanyImpl extends verkehrschaos.TruckCompanyPOA {

    private String           m_name;
    private ArrayList<Truck> m_trucks;
    private ArrayList<Truck> m_arriving;
    private Semaphore        m_running;
    private TruckCompany     m_obj;
    
    public TruckCompanyImpl(final String name) {
    	m_name = name;
    	m_trucks = new ArrayList<Truck>();
    	m_arriving = new ArrayList<Truck>();
    	m_running = new Semaphore(0);
    }
    
    public void setObj(final TruckCompany obj) {
    	m_obj = obj;
    }
    
    public void run(final NamingContextExt ncontext, final String pos,
                    final String streets_name) {
    	// Register for location
    	Streets streets = null;
    	try {
            org.omg.CORBA.Object obj = ncontext.resolve_str(streets_name);
    		streets = StreetsHelper.narrow(obj);
    		streets.claim(m_obj, pos);
			m_running.acquire();
		} catch (InterruptedException | ELocationNotFound | ELocationInUse | NotFound | CannotProceed | InvalidName e) {
			e.printStackTrace();
		} finally {
			if (streets != null) {
				try {
					streets.free(pos);
				} catch (ELocationNotFound e) {
					e.printStackTrace();
				}
			}
		}
    }
	
	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public void addTruck(Truck truck) {
		synchronized(m_trucks) {
			m_trucks.add(truck);
		}
	}

	@Override
	public void removeTruck(Truck truck) {
		synchronized(m_trucks) {
			if (m_trucks.contains(truck)) {
				m_trucks.remove(truck);
			}
		}
		synchronized(m_arriving) {
			if (m_arriving.contains(truck)) {
				m_arriving.remove(truck);
			}
		}
	}

	@Override
	public int getTrucks(TTruckListHolder trucks) {
		// TODO abgefahren trucks dürfen hier nicht mehr drin sein...
		synchronized(m_trucks) {
			trucks.value = new Truck[m_trucks.size()];
			if (!m_trucks.isEmpty()) {
				m_trucks.toArray(trucks.value);
			}
		    return m_trucks == null ? 0 : m_trucks.size();
		}
	}

	@Override
	public void leave(Truck truck) {
		synchronized(m_trucks) {
			m_trucks.remove(truck);
		}
	}

	@Override
	public void advise(Truck truck) {
		truck.setCompany(m_obj);
		synchronized(m_arriving) {
			m_arriving.add(truck);
		}
	}

	@Override
	public void arrive(Truck truck) {
		synchronized(m_arriving) {
			m_arriving.remove(truck);
		}
	}

	@Override
	public void putOutOfService() {
		synchronized(m_arriving) {
			for (Truck t : m_arriving) {
				t.putOutOfService();
			}
			m_arriving.clear();
		}
		synchronized(m_trucks) {
			for (Truck t : m_trucks) {
				t.putOutOfService();
			}
			m_trucks.clear();
		}
		m_running.release();
	}
}