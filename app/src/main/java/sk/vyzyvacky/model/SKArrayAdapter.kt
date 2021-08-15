package sk.vyzyvacky.model

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import java.util.*

/**
 * A ListAdapter that manages a ListView backed by an array of arbitrary
 * objects.  By default this class expects that the provided resource id references
 * a single TextView.  If you want to use a more complex layout, use the constructors that
 * also takes a field id.  That field id should reference a TextView in the larger layout
 * resource.
 *
 *
 * However the TextView is referenced, it will be filled with the toString() of each object in
 * the array. You can add lists or arrays of custom objects. Override the toString() method
 * of your objects to determine what text will be displayed for the item in the list.
 *
 *
 * To use something other than TextViews for the array display, for instance, ImageViews,
 * or to have some of data besides toString() results fill the views,
 * override [.getView] to return the type of view you want.
 */
class SKArrayAdapter<T>(context: Context, textViewResourceId: Int, objects: Array<T>) :
    BaseAdapter(), Filterable {
    /**
     * Lock used to modify the content of [.mObjects]. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see [.getFilter] to make a synchronized copy of
     * the original array of data.
     */
    private val mLock = Any()

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private var mObjects: List<T>? = null

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private var mResource = 0

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private var mDropDownResource = 0

    /**
     * If the inflated resource is not a TextView, {#mFieldId} is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private var mFieldId = 0

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    var context: Context? = null
        private set
    private var mOriginalValues: ArrayList<T>? = null
    private var mFilter: SKArrayFilter? = null
    private var mInflater: LayoutInflater? = null

    private fun init(context: Context, resource: Int, objects: List<T>) {
        this.context = context
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mDropDownResource = resource
        mResource = mDropDownResource
        mObjects = objects
        mFieldId = 0
    }

    /**
     * {@inheritDoc}
     */
    override fun getCount(): Int {
        return mObjects!!.size
    }

    /**
     * {@inheritDoc}
     */
    override fun getItem(position: Int): T {
        return mObjects!![position]
    }

    /**
     * {@inheritDoc}
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * {@inheritDoc}
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mResource)
    }

    private fun createViewFromResource(
        position: Int, convertView: View?, parent: ViewGroup,
        resource: Int,
    ): View {
        val view: View = convertView ?: mInflater!!.inflate(resource, parent, false)
        val text: TextView = try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                view as TextView
            } else {
                //  Otherwise, find the TextView field within the layout
                view.findViewById(mFieldId)
            }
        } catch (e: ClassCastException) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView")
            throw IllegalStateException(
                "ArrayAdapter requires the resource ID to be a TextView", e)
        }
        text.text = getItem(position).toString()
        return view
    }

    /**
     * {@inheritDoc}
     */
    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mDropDownResource)
    }

    /**
     * {@inheritDoc}
     */
    override fun getFilter(): SKArrayFilter {
        if (mFilter == null) {
            mFilter = SKArrayFilter()
        }
        return mFilter!!
    }

    /**
     *
     * An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.
     */
    inner class SKArrayFilter : Filter() {
        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()
            if (mOriginalValues == null) {
                synchronized(mLock) { mOriginalValues = mObjects?.toMutableList() as ArrayList<T>? }
            }
            if (prefix != null) {
                if (prefix.isEmpty()) {
                    synchronized(mLock) {
                        val list = mOriginalValues!!
                        results.values = list
                        results.count = list.size
                    }
                } else {
                    val prefixString = prefix.toString().lowercase(Locale.getDefault())
                    val values = mOriginalValues
                    val count = values!!.size
                    val newValues = ArrayList<T>(count)
                    for (i in 0 until count) {
                        val value = values[i]
                        val valueText = value.toString().lowercase(Locale.getDefault())
                        val valueTextNoPalatals = toNoPalatals(valueText)
                        val prefixStringNoPalatals = toNoPalatals(prefixString)

                        // First match against the whole, non-split value
                        if (valueText.startsWith(prefixString) || valueTextNoPalatals.startsWith(
                                prefixStringNoPalatals)
                        ) {
                            newValues.add(value)
                        } else {
                            val words = valueText.split(" ").toTypedArray()
                            for (word in words) {
                                if (word.startsWith(prefixString) || toNoPalatals(word).startsWith(
                                        prefixStringNoPalatals)
                                ) {
                                    newValues.add(value)
                                    break
                                }
                            }
                        }
                    }
                    results.values = newValues
                    results.count = newValues.size
                }
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            mObjects = results?.values as List<T>?
            if (results != null && results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

        private fun toNoPalatals(originalString: String): String {
            var original = originalString
            original = original.replace("Á", "A")
            original = original.replace("Ä", "A")
            original = original.replace("Č", "C")
            original = original.replace("Ď", "D")
            original = original.replace("É", "E")
            original = original.replace("Í", "I")
            original = original.replace("Ĺ", "L")
            original = original.replace("Ľ", "L")
            original = original.replace("Ň", "N")
            original = original.replace("Ó", "O")
            original = original.replace("Ô", "O")
            original = original.replace("Ŕ", "R")
            original = original.replace("Š", "S")
            original = original.replace("Ť", "T")
            original = original.replace("Ú", "U")
            original = original.replace("Ý", "Y")
            original = original.replace("Ž", "Z")
            original = original.replace("á", "a")
            original = original.replace("ä", "a")
            original = original.replace("č", "c")
            original = original.replace("ď", "d")
            original = original.replace("é", "e")
            original = original.replace("í", "i")
            original = original.replace("ĺ", "l")
            original = original.replace("ľ", "l")
            original = original.replace("ň", "n")
            original = original.replace("ó", "o")
            original = original.replace("ô", "o")
            original = original.replace("ŕ", "r")
            original = original.replace("š", "s")
            original = original.replace("ť", "t")
            original = original.replace("ú", "u")
            original = original.replace("ý", "y")
            original = original.replace("ž", "z")
            return original
        }
    }

    init {
        init(context, textViewResourceId, listOf(*objects))
    }
}