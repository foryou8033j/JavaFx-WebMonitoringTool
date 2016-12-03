package mil.navy.monitoring.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ����ó ����Ʈ�� ���δ� ���� Ŭ�����̴�.
 * XML�� �����ϴ� �� ����Ѵ�.
 */
@XmlRootElement(name = "sites")
public class SiteListWrapper {

    private List<Site> sites;

    @XmlElement(name = "site")
    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }
}