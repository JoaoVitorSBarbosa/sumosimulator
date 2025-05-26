package io.sim;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Rota {

	private boolean on;
	private String uriRotaXML;
	private String[] Rota;
	private String idRota;

	public Rota(String _uriRotasXML, String _idRota) {
		this.uriRotaXML = _uriRotasXML;
		this.idRota = _idRota;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(this.uriRotaXML);
			NodeList nList = doc.getElementsByTagName("vehicle");
			
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) nNode;
					String idRotaAux = this.idRota;
					Node node = elem.getElementsByTagName("route").item(0);
					NamedNodeMap nodes = elem.getAttributes();
                    Node nodeID = nodes.item(1);
					Element edges = (Element) node;
					if(this.idRota.equals(nodeID.getNodeValue())) {
						this.Rota = new String[] { idRotaAux, edges.getAttribute("edges") };
					}
				}
			}

			Thread.sleep(100);
			this.on = true;

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public String toString() {
		return idRota;
	}
	public boolean equals(String idString) {
		return idString.equals(idRota);
	}
	public synchronized String getIDRota() {
		return this.idRota;
	}

	public String getUriRotaXML() {
		return this.uriRotaXML;
	}

	public String[] getRota() {
		return this.Rota;
	}

	public String getIdRota() {
		return this.idRota;
	}

	public boolean isOn() {
		return this.on;
	}
}