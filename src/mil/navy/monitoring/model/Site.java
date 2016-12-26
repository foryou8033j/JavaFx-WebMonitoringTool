package mil.navy.monitoring.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Site 
{

	private final StringProperty address;
	private final StringProperty name;
	private final StringProperty refreshTime;
	private final StringProperty running;
	private final StringProperty zoom;
	
	
	public Site() {
		this(null, null, false);
	}
	
	public Site(String name, String address, boolean setRunning)
	{
		if(setRunning) this.running = new SimpleStringProperty("true");
		else this.running = new SimpleStringProperty("false");
		
		this.name = new SimpleStringProperty(name);
		this.address = new SimpleStringProperty(address);
		zoom = new SimpleStringProperty("0.5");
		refreshTime = new SimpleStringProperty("30");
	}
	
	public String getName()
	{
		return name.get();
	}
	
	public String getAddress()
	{
		return address.get();
	}
	
	public double getZoom()
	{
		return Double.valueOf(zoom.get());
	}
	
	public int getRefreshTime()
	{
		return Integer.valueOf(refreshTime.get());
	}
	
	public void setName(String name)
	{
		this.name.set(name);
	}
	
	public void setAddress(String address)
	{
		this.address.set(address);
	}
	
	public void setZoom(double zoom)
	{
		this.zoom.set(String.valueOf(zoom));
	}
	
	public void setRefreshTime(int refreshTime)
	{
		this.refreshTime.set(String.valueOf(refreshTime));
	}
	
	public StringProperty getNameProperty()
	{
		return name;
	}
	
	public StringProperty getAddressProperty()
	{
		return address;
	}
	
	public boolean isRunning()
	{
		if(running.get().equals("true")) return true;
		else return false;
	}
	
	public void setRunning(boolean run)
	{
		if(run) running.set("true");
		else	running.set("false");
	}
	
	
}
