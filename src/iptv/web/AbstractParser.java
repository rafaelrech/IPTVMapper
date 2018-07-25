package iptv.web;

import java.io.IOException;

import org.xml.sax.SAXException;

public abstract class AbstractParser implements Parser {

	protected void parseEntry(PlaylistEntry playlistEntry, Playlist playlist) {
		try {
			AutoDetectParser parser = new AutoDetectParser(); // Should auto-detect!
			parser.parse(playlistEntry.get(PlaylistEntry.URI), playlist);
			return;
		} catch (IOException e) {
		} catch (SAXException e) {
		} catch (JPlaylistParserException e) {
		}

		playlist.add(playlistEntry);
	}

}
