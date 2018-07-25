package iptv.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.xml.sax.SAXException;

public class AutoDetectParser {

	public void parse(String uri, String mimeType, InputStream stream, Playlist playlist)
			throws IOException, SAXException, JPlaylistParserException {
		Parser parser = null;
		String extension = null;

		if (uri == null) {
			throw new IllegalArgumentException("URI cannot be NULL");
		}

		if (stream == null) {
			throw new IllegalArgumentException("stream cannot be NULL");
		}

		if (mimeType == null) {
			mimeType = "";
		}

		if (mimeType.split("\\;").length > 0) {
			mimeType = mimeType.split("\\;")[0];
		}

		// ASXPlaylistParser asxPlaylistParser = new ASXPlaylistParser();
		M3UPlaylistParser m3uPlaylistParser = new M3UPlaylistParser();
		M3U8PlaylistParser m3u8PlaylistParser = new M3U8PlaylistParser();
		// PLSPlaylistParser plsPlaylistParser = new PLSPlaylistParser();
		// XSPFPlaylistParser xspfPlaylistParser = new XSPFPlaylistParser();

		extension = getFileExtension(uri);

		// if (extension.equalsIgnoreCase(ASXPlaylistParser.EXTENSION)
		// ||
		// asxPlaylistParser.getSupportedTypes().contains(MediaType.parse(mimeType)))
		// {
		// parser = asxPlaylistParser;
		// } else
		if (extension.equalsIgnoreCase(M3UPlaylistParser.EXTENSION)
				|| (m3uPlaylistParser.getSupportedTypes().contains(MediaType.parse(mimeType))
						&& !extension.equalsIgnoreCase(M3U8PlaylistParser.EXTENSION))) {
			parser = m3uPlaylistParser;
		} else if (extension.equalsIgnoreCase(M3U8PlaylistParser.EXTENSION)
				|| m3uPlaylistParser.getSupportedTypes().contains(MediaType.parse(mimeType))) {
			parser = m3u8PlaylistParser;
			// } else if
			// (extension.equalsIgnoreCase(PLSPlaylistParser.EXTENSION)
			// ||
			// plsPlaylistParser.getSupportedTypes().contains(MediaType.parse(mimeType)))
			// {
			// parser = plsPlaylistParser;
			// } else if
			// (extension.equalsIgnoreCase(XSPFPlaylistParser.EXTENSION)
			// ||
			// xspfPlaylistParser.getSupportedTypes().contains(MediaType.parse(mimeType)))
			// {
			// parser = xspfPlaylistParser;
		} else {
			throw new JPlaylistParserException("Unsupported format");
		}

		parser.parse(uri, stream, playlist);
	}

	public void parse(String uri, Playlist playlist) throws IOException, SAXException, JPlaylistParserException {
		Parser parser = null;
		String extension = null;

		if (uri == null) {
			throw new IllegalArgumentException("URI cannot be NULL");
		}

		// ASXPlaylistParser asxPlaylistParser = new ASXPlaylistParser();
		M3UPlaylistParser m3uPlaylistParser = new M3UPlaylistParser();
		M3U8PlaylistParser m3u8PlaylistParser = new M3U8PlaylistParser();
		// PLSPlaylistParser plsPlaylistParser = new PLSPlaylistParser();
		// XSPFPlaylistParser xspfPlaylistParser = new XSPFPlaylistParser();

		extension = getFileExtension(uri);

		// if (extension.equalsIgnoreCase(ASXPlaylistParser.EXTENSION)) {
		// parser = asxPlaylistParser;
		// } else
		if (extension.equalsIgnoreCase(M3UPlaylistParser.EXTENSION)
				&& !extension.equalsIgnoreCase(M3U8PlaylistParser.EXTENSION)) {
			parser = m3uPlaylistParser;
		} else if (extension.equalsIgnoreCase(M3U8PlaylistParser.EXTENSION)) {
			parser = m3u8PlaylistParser;
			// } else if
			// (extension.equalsIgnoreCase(PLSPlaylistParser.EXTENSION)) {
			// parser = plsPlaylistParser;
			// } else if
			// (extension.equalsIgnoreCase(XSPFPlaylistParser.EXTENSION)) {
			// parser = xspfPlaylistParser;
		} else {
			throw new JPlaylistParserException("Unsupported format");
		}

		URL url;
		HttpURLConnection conn = null;
		InputStream is = null;

		try {
			url = new URL(URLDecoder.decode(uri, "UTF-8"));
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(6000);
			conn.setReadTimeout(6000);
			conn.setRequestMethod("GET");

			is = conn.getInputStream();

			parser.parse(url.toString(), is, playlist);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private String getFileExtension(String uri) {
		String fileExtention = "";

		int index = uri.lastIndexOf(".");

		if (index != -1) {
			fileExtention = uri.substring(index, uri.length());
		}

		return fileExtention;
	}
}