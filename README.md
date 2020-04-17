# ImageCrop
This is an Android project base on MVVM architecture cross UI and database to implement image cropping, store and present.
The technique composed of the following.

**Main Page**
- Model- query database via Room, composed of Dao, Entity, RoomDatabase
- View- Activity, Fragment, composed of CoordinatorLayout, RecycleView, CardView...
- ViewModel- do asynchronous work via Coroutine, composed of GlobalScope.launch, MutableLiveData…

**Cropping Page**
- Model- query database via Room, composed of Dao, Entity, RoomDatabase…
- View- Activity, Customize view
- ViewModel- do asynchronous work via RxJava, composed of Single, Completable

