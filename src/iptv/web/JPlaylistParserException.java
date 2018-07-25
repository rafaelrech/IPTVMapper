package iptv.web;

public class JPlaylistParserException extends Exception {

	private static final long serialVersionUID = 1L;

	public JPlaylistParserException(String msg) {
        super(msg);
    }

    public JPlaylistParserException(String msg, Throwable cause) {
        super(msg, cause);
    }

}