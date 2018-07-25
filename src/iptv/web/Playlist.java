package iptv.web;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

	private List<PlaylistEntry> mPlaylistEntries = null;

	/**
	 * Constructs a new, empty playlist.
	 */
	public Playlist() {
		setPlaylistEntries(new ArrayList<PlaylistEntry>());
	}

	public void add(PlaylistEntry playlistEntry) {
		mPlaylistEntries.add(playlistEntry);
	}

	/**
	 * @return the mPlaylistEntries
	 */
	public List<PlaylistEntry> getPlaylistEntries() {
		return mPlaylistEntries;
	}

	/**
	 * @param mPlaylistEntries
	 *            the mPlaylistEntries to set
	 */
	protected void setPlaylistEntries(List<PlaylistEntry> mPlaylistEntries) {
		this.mPlaylistEntries = mPlaylistEntries;
	}
}
