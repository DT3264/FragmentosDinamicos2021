package net.ivanvega.fragmentosdinamicos;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean desdeServicio = getIntent().getBooleanExtra("flag_servicio", false);
        if (desdeServicio) {
            mostrarDetalle(-1, true);
        } else {
            if (findViewById(R.id.contenedor_pequeno) != null &&
                    getSupportFragmentManager()
                            .findFragmentById(R.id.contenedor_pequeno) == null
            ) {

                getSupportFragmentManager().beginTransaction().
                        setReorderingAllowed(true)
                        .add(
                                R.id.contenedor_pequeno,
                                SelectorFragment.class, null).commit();

            }
        }
    }

    public void mostrarDetalle(int pos, boolean desdeServicio) {
        DetalleFragment detalleFragment = new DetalleFragment();
        Bundle bundle = new Bundle();

        bundle.putBoolean(DetalleFragment.ARG_SERVICIO, desdeServicio);

        bundle.putInt(DetalleFragment.ARG_INDEX_LIBRO, pos);

        detalleFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().
                setReorderingAllowed(true)
                .replace(R.id.contenedor_pequeno, detalleFragment)
                .addToBackStack(null)
                .commit();
    }
}