package iptv.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.xml.sax.SAXException;

public interface Parser {

	Set<MediaType> getSupportedTypes();

	void parse(String uri, InputStream stream, Playlist playlist)
			throws IOException, SAXException, JPlaylistParserException;

}