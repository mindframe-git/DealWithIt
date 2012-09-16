package com.mindframe.dealwithit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * 
 * @author mindframe
 * 
 */
public class DealWithIt extends Activity {
	Intent intent = new Intent();
	private static final int SELECT_PICTURE = 2;
	Uri imgSelected;
	ImageButton imgBtn;
	File file;
	Bitmap photo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindowManager().getDefaultDisplay().getHeight();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deal_w);

		Log.d("Metodo:", "Entramos:");

		imgBtn = (ImageButton) findViewById(R.id.imgBtn);

		imgBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Este intent recoge una imagen de la galería
				intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				startActivityForResult(intent, SELECT_PICTURE);
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("Método", "onActivityResult");

		if (resultCode == -1) {
			imgSelected = data.getData();

			// Para trabajar con el Facedetector hay que aplicar esta
			// configuración a la imagen a tratar.
			BitmapFactory.Options bmfOpt = new BitmapFactory.Options();
			bmfOpt.inPreferredConfig = Bitmap.Config.RGB_565;

			InputStream is;
			try {
				is = getContentResolver().openInputStream(imgSelected);
				BufferedInputStream bis = new BufferedInputStream(is);
				photo = BitmapFactory.decodeStream(bis, new Rect(), bmfOpt);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			setContentView(new Panel(this, photo));
		} else {
			setContentView(R.layout.activity_deal_w);
			imgBtn = (ImageButton) findViewById(R.id.imgBtn);

			imgBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Este intent recoge una imagen de la galería
					intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					startActivityForResult(intent, SELECT_PICTURE);
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.menu_deal_w, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.OptRepeat:
			// Para repetir, recargamos el SurfaceView. photo va a ser la misma
			// ya que estaba cargada.
			setContentView(new Panel(this, photo));
			return true;

		case R.id.OptAnother:
			// Para elegir otra imagen, llamamos directamente al intent de
			// seleccionar de la galería
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			startActivityForResult(intent, SELECT_PICTURE);

			return true;
		case R.id.OptDownload:
			// Tareas pendientes :)
			Toast.makeText(this, "Todavía no se ha implementado esta función.", Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	class Panel extends SurfaceView implements SurfaceHolder.Callback {

		private static final int MAXNUMFACES = 10;
		private static final int SPEED = 5;

		private FaceDetector detector;
		private Face[] faces;
		List<PointF> eyeList;
		PointF puntoGafas;
		Bitmap gafas, imgBackg, dealText;
		int w_backg, h_backg, facesFound, w_screen, h_screen;
		int dynamic_y = 0;
		MediaPlayer mp;
		boolean foundGlasses = false;

		List<Glasses> glassesList = new ArrayList<Glasses>();
		Glasses currentGlasses;

		Paint paint = new Paint();

		Thread thread;

		SurfaceHolder holder;

		public Panel(Context context, Bitmap photo) {
			super(context);

			Log.d("Metodo:", "Constructor");
			holder = getHolder();
			holder.addCallback(this);
			thread = new ThreadPrincipal(holder, this);
			if (photo != null)
				imgBackg = photo;

			// Cargamos el archivo de la música
			mp = MediaPlayer.create(context, R.raw.guitar);
			setDrawingCacheEnabled(true);

		}

		public void populateGlasses() {

			// Recogemos dimensiones de la pantalla
			w_screen = holder.getSurfaceFrame().right;
			h_screen = holder.getSurfaceFrame().bottom;

			// Redimensionamos el texto en función de la pantalla
			dealText = BitmapFactory.decodeResource(getResources(), R.drawable.dealtext);
			dealText = resizeBitmap(w_screen, dealText);

			// Redimensionamos la imagen de fondo en función de la pantalla
			imgBackg = resizeBitmap(w_screen, imgBackg);

			w_backg = imgBackg.getWidth();
			h_backg = imgBackg.getHeight();

			faces = new FaceDetector.Face[MAXNUMFACES];
			detector = new FaceDetector(w_backg, h_backg, MAXNUMFACES);
			facesFound = detector.findFaces(imgBackg, faces);

			// recorremos las caras detectadas y nos creamos el objeto Glasses,
			// con sus coordenadas, sus gafas redimensionadas...
			for (int i = 0; i <= faces.length - 1; i++) {
				if (faces[i] != null) {
					foundGlasses = true;
					Glasses glasses = new Glasses();
					PointF center = new PointF();
					faces[i].getMidPoint(center);
					glasses.pos_x = (int) (center.x - faces[i].eyesDistance() - faces[i].eyesDistance() / 2.5);
					glasses.pos_y = (int) (center.y - faces[i].eyesDistance() / 4);
					glasses.distance = faces[i].eyesDistance();
					glasses.glasses = resizeGlasses(faces[i].eyesDistance());
					glasses.num = String.valueOf(i);
					glassesList.add(glasses);
				}
			}
		}

		@Override
		public void onDraw(Canvas canvas) {
			Log.d("Metodo:", "onDraw");
			canvas.drawBitmap(imgBackg, 0, 0, paint);
			// Va dibujando las gafas según la posición
			for (Glasses glasses : glassesList) {
				currentGlasses = glasses;
				if (currentGlasses != null) {
					canvas.drawBitmap(currentGlasses.glasses, currentGlasses.pos_x, currentGlasses.dynamic_y, null);
				}
			}
			// Una vez acabe de dibujar las gafas, dibuja el texto y reproduce
			// el sonido.
			if (!contThread()) {
				canvas.drawBitmap(dealText, (w_backg - dealText.getWidth()) / 2, h_backg - 100, paint);
				mp.start();
			}

		}

		public void drawBackg(Canvas c) {
			// En el caso de que no detecte ninguna cara, pintamos sólo el fondo
			// y mostramos un mensaje
			Log.d("Pinta", "Fondo");
			// Toast.makeText(getContext(), "No se ha detectado ninguna cara",
			// Toast.LENGTH_LONG).show();
			c.drawBitmap(imgBackg, 0, 0, paint);

		}

		public boolean contThread() {
			// Recorre la lista de gafas. Si encuentra alguna que no haya
			// llegado al final, se sigue ejecutando el thread
			boolean cont = false;
			for (Glasses glases : glassesList) {
				if (glases.dynamic_y <= glases.pos_y) {
					cont = true;
				}
			}
			return cont;
		}

		public void animateGlasses() {
			Log.d("Metodo:", "animateGlasses");
			// Recorre la lista de gafas, aumentando dynamic_y en SPEED puntos
			// hasta llegar a su posición final
			for (Glasses glasses : glassesList) {
				currentGlasses = glasses;
				if (currentGlasses != null) {
					if (currentGlasses.dynamic_y <= currentGlasses.pos_y) {
						currentGlasses.dynamic_y += SPEED;
					}
				}
			}
		}

		public Bitmap resizeGlasses(float distance) {
			// Redimensiona las gafas en función a la distancia entre los ojos.
			Bitmap gafasOrig = BitmapFactory.decodeResource(getResources(), R.drawable.glasses);

			int w_gafOrig = gafasOrig.getWidth();
			int h_gafOrig = gafasOrig.getHeight();

			float w_escala = (float) (distance * 2.3);
			float h_escala = (float) (h_gafOrig * distance / w_gafOrig * 2.3);

			return Bitmap.createScaledBitmap(gafasOrig, (int) w_escala, (int) h_escala, true);

		}

		public Bitmap resizeBitmap(float newWidth, Bitmap img) {

			// Redimensiona un bitmap en función al ancho que se le pase.

			float w_img = img.getWidth();
			float h_img = img.getHeight();

			float newHeight = h_img * newWidth / w_img;

			return Bitmap.createScaledBitmap(img, (int) newWidth, (int) newHeight, true);

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Log.d("Metodo:", "surfaceChanged");

		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d("Metodo:", "surfaceCreated");
			// En cuanto se crea el surface, empezamos la acción y lanzamos el
			// hilo.
			populateGlasses();
			thread.start();

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d("Metodo:", "surfaceDestroyed");
			// Terminamos el hilo una vez destruida la pantalla
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {
					Log.d("exc", e.getMessage());
				}
			}
		}

		class ThreadPrincipal extends Thread {
			private SurfaceHolder _surfaceHolder;
			boolean running = true;
			Panel _panel;

			public ThreadPrincipal(SurfaceHolder surfaceHolder, Panel panel) {
				Log.d("Metodo:", "ConstructorThread");
				_surfaceHolder = surfaceHolder;
				_panel = panel;

			}

			@Override
			public void run() {
				Log.d("Metodo:", "ThreadRun");
				Canvas c = null;
				while (_panel.contThread()) {
					// Ejecutamos los métodos de animar y dibujar a cada paso
					// mientras no hayan llegado todas las gafas a su punto
					// final
					try {
						c = _surfaceHolder.lockCanvas();
						synchronized (_surfaceHolder) {
							_panel.animateGlasses();
							_panel.onDraw(c);
						}
					} finally {
						if (c != null)
							_surfaceHolder.unlockCanvasAndPost(c);
					}
				}
				if (!_panel.foundGlasses) {
					// Si no ha encontrado gafas, dibujamos sólo el fondo.
					try {
						c = _surfaceHolder.lockCanvas();
						synchronized (_surfaceHolder) {
							_panel.drawBackg(c);
						}
					} finally {
						if (c != null)
							_surfaceHolder.unlockCanvasAndPost(c);
					}
				}

			}
		}

	}
}
