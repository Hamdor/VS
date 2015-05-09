package coordinator;

import java.util.ArrayList;

public class coordinatorImpl extends CoordinatorPOA {
	
	private String m_name;
	private ArrayList<String> m_workers;
	private ArrayList<String> m_starters;
	
	public coordinatorImpl(final String name) {
		m_name     = name;
		m_workers  = new ArrayList<String>();
		m_starters = new ArrayList<String>();
	}

	@Override
	public void register(String whom) {
		synchronized(m_workers) {
			if (!m_workers.contains(whom)) {
				m_workers.add(whom);
			}
		}
	}
	
	@Override
	public void register_starter(String whom) {
		synchronized(m_starters) {
			if (!m_starters.contains(whom)) {
				m_starters.add(whom);
			}
		}
	}

	@Override
	public void inform(String whom, int seqNr, boolean finished, int current) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getStarter() {
		synchronized(m_workers) {
		    return m_workers.toArray(new String[m_workers.size()]);
		}
	}

	@Override
	public void calculate(String monitor, int ggTLower, int ggTUpper,
			int delayLower, int delayUpper, int period, int expectedggT) {
		// TODO Auto-generated method stub
	}

	@Override
	public void kill(String whom) {
		// TODO Auto-generated method stub
		
	}
}
