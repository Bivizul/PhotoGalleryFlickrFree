package com.bivizul.photogalleryflickrfree

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment : Fragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView

    /** Можно указать любой тип для общего аргумента ThumbnailDownloader. Однако,
    это должен быть тип объекта, который будет использоваться в качестве идентификатора
    для загрузки. В нашем случае удобно использовать PhotoHolder, так как он также
    является местом, куда в конечном итоге будут направлены загруженные изображения */
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Сохраняем PhotoGalleryFragment таким образом, чтобы жизнь экземпляра фрагмента
        соответствовала предполагаемой пользователем жизни фрагмента

        Обычно следует избегать сохранения фрагментов. Мы будем делать это только
        здесь, потому что сохранение фрагмента упрощает реализацию и позволяет нам
        сконцентрироваться на изучении того, как работает HandlerThread */
        retainInstance = true
        setHasOptionsMenu(true)

        // Получение экземпляра ViewModel от провайдера
        photoGalleryViewModel = ViewModelProvider(this)[PhotoGalleryViewModel::class.java]

        /** Создаем экземпляр ThumbnailDownloader и подписываем его на получение обратных вызовов
        жизненного цикла из PhotoGalleryFragment */
        val responseHandler = Handler()
        /** Подключение к обработчику ответа
        Теперь ThumbnailDownloader имеет доступ к экземпляру Handler, связанному
        с экземпляром Looper главного потока, через поле responseHandler. В нем также есть
        реализация типа функции для реализации интерфейса при возврате Bitmap. В частности,
        функция, переданная в функцию высшего порядка onThumbnailDownloaded, устанавливает
        Drawable запрошенного PhotoHolder на только что загруженный Bitmap */
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        /** Добавление рабочих ограничений */
        val constrants = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        /** Планирование WorkRequest */
        val workRequest = OneTimeWorkRequest
            .Builder(PollWorker::class.java)
            .setConstraints(constrants)
            .build()
        WorkManager.getInstance().enqueue(workRequest)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        // Установливаем layoutManager утилизатора на новый экземпляр GridLayoutManager.
        // Пока что просто зададим количество столбцов равным 3
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
    }

    // Наблюдение за LiveData ViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            // гарантия, что объект LiveData удалит наблюдателя при уничтожении представления фрагмента
            viewLifecycleOwner,
            // Добавление адаптера для наблюдения за доступностью и изменением данных
            Observer { galleryItems ->
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            }
        )
    }

    /** Отказ от регистрации наблюдателя жизненного цикла представления */
    override fun onDestroyView() {
        super.onDestroyView()
        thumbnailDownloader.clearQueue()
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        /** Регистрация событий SearchView.OnQueryTextListener */
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                /** Выполняется тогда, когда пользователь отправляет запрос. Запрос, заданный
                 * пользователем, передается в качестве входного */
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)
                    return true
                }

                /** Выполняется при изменении текста в текстовом поле SearchView.
                 * Это означает, что функция вызывается каждый раз, когда меняется один символ */
                override fun onQueryTextChange(queryText: String?): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })
            /** Предварительное заполнение SearchView */
            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }
    }

    /** Очистка сохраненного запроса */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private class PhotoHolder(itemImageView: ImageView) : RecyclerView.ViewHolder(itemImageView) {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
    }

    // Выдает PhotoHolder из галереи
    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(
                R.layout.list_item_gallery,
                parent,
                false
            ) as ImageView
            return PhotoHolder(view)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            // Назначение временного изображения
            val placeholder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.picture
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}