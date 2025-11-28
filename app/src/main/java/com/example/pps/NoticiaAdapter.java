package com.example.pps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoticiaAdapter extends RecyclerView.Adapter<NoticiaAdapter.NoticiaViewHolder> {

    private List<Noticia> noticiasList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Interface para el click listener
    public interface OnItemClickListener {
        void onItemClick(Noticia noticia);
    }

    private OnItemClickListener onItemClickListener;

    public NoticiaAdapter(List<Noticia> noticiasList) {
        this.noticiasList = noticiasList;
    }

    // Método para establecer el click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public NoticiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_noticia, parent, false);
        return new NoticiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticiaViewHolder holder, int position) {
        Noticia noticia = noticiasList.get(position);
        holder.bind(noticia);

        // Configurar el click listener en cada item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(noticia);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noticiasList.size();
    }

    public class NoticiaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitulo, tvResumen, tvAutor, tvFecha;

        public NoticiaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvResumen = itemView.findViewById(R.id.tvResumen);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }

        public void bind(Noticia noticia) {
            tvTitulo.setText(noticia.getTitulo());
            tvResumen.setText(noticia.getResumen());
            tvAutor.setText("Por: " + noticia.getAutor());

            if (noticia.getFecha() != null) {
                tvFecha.setText(dateFormat.format(noticia.getFecha()));
            }
        }
    }
}