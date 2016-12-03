package mil.navy.monitoring.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 연락처 리스트를 감싸는 헬퍼 클래스이다.
 * XML로 저장하는 데 사용한다.
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