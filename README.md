AutoPreference
==============

Automatically saves annotated attributes of an object, using the mechanism of 'SharedPreference'

```
public class myFragment extends Fragment implements
{
    @PreferenceStore(name="pageIndex")
    public int mPageIndex;
    
    @PreferenceStore(name="pageNumber")
    public int mPageNumber;
    
    @PreferenceStore(name="siteName")
    public String mSiteNameSelected;
    ...
    
    @Override
    public void onStart() {
        super.onStart();
        
        autoPreference = new AutoPreference(getActivity(), TAG, Context.MODE_PRIVATE);
        autoPreference.parser(this);
        autoPreference.restore();  // update all 'PreferenceStore'
}
```