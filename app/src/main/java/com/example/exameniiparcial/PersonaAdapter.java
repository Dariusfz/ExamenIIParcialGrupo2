package com.example.exameniiparcial;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PersonaAdapter extends RecyclerView.Adapter<PersonaAdapter.PersonaViewHolder> {
    private List<PersonaLista> personas;
    private List<PersonaLista> personasFiltradas;
    private Context context;
    private int selectedPosition = -1;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(PersonaLista persona);
    }

    private OnLongItemClickListener longItemClickListener;

    public void setOnLongItemClickListener(OnLongItemClickListener listener) {
        this.longItemClickListener = listener;
    }

    public PersonaAdapter(List<PersonaLista> personas, Context context) {
        this.personas = personas;
        this.personasFiltradas = new ArrayList<>(personas);
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public PersonaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_detalle_contacto, parent, false);
        return new PersonaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonaViewHolder holder, int position) {
        PersonaLista persona = personasFiltradas.get(position);
        holder.bind(persona, position);
    }

    @Override
    public int getItemCount() {
        return personasFiltradas != null ? personasFiltradas.size() : 0;
    }

    public class PersonaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFirma;
        TextView textViewNombre;

        public PersonaViewHolder(View itemView) {
            super(itemView);
            imageViewFirma = itemView.findViewById(R.id.imageViewFirma);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
        }

        public void bind(PersonaLista persona, int position) {
            textViewNombre.setText(persona.getNombre_completo());

            // Decodificar Base64 a Bitmap
            if(persona.getFirma_digital() != null && !persona.getFirma_digital().isEmpty()) {
                try {
                    String pureBase64 = persona.getFirma_digital();
                    if(pureBase64.contains(",")) {
                        pureBase64 = pureBase64.split(",")[1];
                    }

                    byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    if(decodedBitmap != null) {

                        if (decodedBitmap.getWidth() > 0 && decodedBitmap.getHeight() > 0) {

                            int targetWidth = imageViewFirma.getWidth() > 0 ?
                                    imageViewFirma.getWidth() : 300;
                            int targetHeight = (int) (targetWidth * ((float)decodedBitmap.getHeight()/decodedBitmap.getWidth()));

                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                                    decodedBitmap,
                                    targetWidth,
                                    targetHeight,
                                    true
                            );
                            imageViewFirma.setImageBitmap(scaledBitmap);
                        } else {
                            imageViewFirma.setImageBitmap(decodedBitmap);
                        }
                    } else {
                        imageViewFirma.setImageResource(R.drawable.ic_launcher_background);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    imageViewFirma.setImageResource(R.drawable.imagendefecto);
                }
            } else {
                imageViewFirma.setImageResource(R.drawable.imagendefecto);
            }



            itemView.setBackgroundColor(
                    selectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT
            );

            itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = position;


                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longItemClickListener != null) {
                    longItemClickListener.onLongItemClick(persona);
                    return true;
                }
                return false;
            });
        }
    }


    public PersonaLista getSelectedPersona() {
        try {
            if (selectedPosition != -1 &&
                    selectedPosition < personasFiltradas.size() &&
                    personasFiltradas != null) {
                return personasFiltradas.get(selectedPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Filtrado de búsqueda
    public void filter(String query) {
        personasFiltradas = new ArrayList<>();

        if (personas == null) return;

        if (query.isEmpty()) {
            personasFiltradas.addAll(personas);
        } else {
            query = query.toLowerCase();
            for (PersonaLista item : personas) {
                if (item.getNombre_completo().toLowerCase().contains(query)) {
                    personasFiltradas.add(item);
                }
            }
        }
        selectedPosition = -1;
        notifyDataSetChanged();
    }


    public void updateData(List<PersonaLista> nuevasPersonas) {
        personas.clear();
        personas.addAll(nuevasPersonas);
        personasFiltradas.clear();
        personasFiltradas.addAll(nuevasPersonas);
        notifyDataSetChanged();
    }
    public PersonaLista getPersonaAtPosition(int position) {
        if (position >= 0 && position < personasFiltradas.size()) {
            return personasFiltradas.get(position);
        }
        return null;
    }
}