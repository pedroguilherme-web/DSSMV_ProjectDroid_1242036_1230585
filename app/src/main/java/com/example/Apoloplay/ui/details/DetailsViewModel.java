package com.example.Apoloplay.ui.details;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Apoloplay.data.ServiceLocator;
import com.example.Apoloplay.data.remote.SpotifyService;
import com.example.Apoloplay.data.remote.dto.PlaylistTrackItemDTO;
import com.example.Apoloplay.data.remote.dto.PlaylistTracksResponseDTO;
import com.example.Apoloplay.data.remote.dto.search.SpotifySearchResponseDTO;
import com.example.Apoloplay.data.model.Music;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class DetailsViewModel extends ViewModel {

    private final MutableLiveData<DetailsUiState> _state = new MutableLiveData<>();
    public LiveData<DetailsUiState> getState(){ return _state; }

    private final SpotifyService api = ServiceLocator.spotifyService();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public void init(Music initial){
        if (_state.getValue()==null) _state.setValue(DetailsUiState.idle(initial));
    }

    /** Enriquecer com o 1º resultado do Spotify se faltar URI/capa/álbum */
    public void enrichIfNeeded(String title, String artist){
        DetailsUiState cur = _state.getValue();
        if (cur==null || title==null || artist==null) return;
        Music m = cur.music;
        if (m!=null && m.getSpotifyTrackUri()!=null && !m.getSpotifyTrackUri().isEmpty()) return;

        _state.postValue(DetailsUiState.loading(m));
        final String q = buildQuery(title, artist);
        final String bearer = "Bearer " + ServiceLocator.sessionProvider().getUserAccessToken();

        io.execute(() -> {
            try {
                Response<SpotifySearchResponseDTO> resp = api.searchTracks(bearer, q, "track", 1).execute();
                if (!resp.isSuccessful() || resp.body()==null || resp.body().tracks==null
                        || resp.body().tracks.items==null || resp.body().tracks.items.isEmpty()){
                    _state.postValue(DetailsUiState.data(m,false)); // mantém o que havia
                    return;
                }
                SpotifySearchResponseDTO.Item it = resp.body().tracks.items.get(0);

                Music enriched = new Music(
                        it.name != null ? it.name : m.getTitle(),
                        (it.artists!=null && !it.artists.isEmpty() && it.artists.get(0)!=null) ? it.artists.get(0).name : m.getArtist(),
                        (it.album!=null && it.album.images!=null && !it.album.images.isEmpty() && it.album.images.get(0)!=null) ? it.album.images.get(0).url : m.getImageUrl(),
                        it.previewUrl,
                        it.album!=null ? it.album.name : m.getAlbumName(),
                        it.album!=null ? it.album.releaseDate : m.getReleaseDate(),
                        it.uri // spotify:track:...
                );
                _state.postValue(DetailsUiState.data(enriched,false));
            } catch (Exception e){
                _state.postValue(DetailsUiState.error(m, "Falha a pesquisar no Spotify"));
            }
        });
    }

    /** Verifica se a faixa já existe NA playlist dada (para mostrar “Remover”). */
    public void checkInPlaylist(String playlistId){
        DetailsUiState cur = _state.getValue();
        if (cur==null || playlistId==null) return;
        Music m = cur.music;
        if (m==null || m.getSpotifyTrackUri()==null || m.getSpotifyTrackUri().isEmpty()){
            // sem URI não há como verificar
            return;
        }
        final String bearer = "Bearer " + ServiceLocator.sessionProvider().getUserAccessToken();
        final String targetUri = m.getSpotifyTrackUri();

        io.execute(() -> {
            boolean found = false; int limit=100, offset=0;
            try{
                while (!found){
                    Response<PlaylistTracksResponseDTO> r =
                            api.getPlaylistTracks(bearer, playlistId, limit, offset).execute();
                    if (!r.isSuccessful() || r.body()==null || r.body().items==null) break;
                    List<PlaylistTrackItemDTO> items = r.body().items;
                    if (items.isEmpty()) break;
                    for (PlaylistTrackItemDTO it : items){
                        if (it!=null && it.track!=null && targetUri.equals(it.track.uri)){ found=true; break; }
                    }
                    if (found || items.size()<limit) break;
                    offset += limit;
                }
            } catch (Exception ignored){}
            _state.postValue(DetailsUiState.data(m, found));
        });
    }

    private String buildQuery(String title, String artist){
        String t = title!=null ? title.replaceAll("\\(.*?\\)", "").trim() : "";
        String a = artist!=null ? artist.replaceAll("\\(.*?\\)", "").trim() : "";
        return "track:\""+t+"\" artist:\""+a+"\"";
    }

    @Override protected void onCleared() { io.shutdownNow(); }
}
