package ca.bischke.apps.filevault;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListViewHolder>
{
    private Context context;
    private List<FileListData> fileDataList;
    private FileListListener fileListListener;

    public FileListAdapter(Context context, List<FileListData> fileDataList, FileListListener fileListListener)
    {
        this.context = context;
        this.fileDataList = fileDataList;
        this.fileListListener = fileListListener;
    }

    @NonNull
    @Override
    public FileListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_list, viewGroup, false);
        return new FileListViewHolder(view, fileListListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileListViewHolder fileViewHolder, int i)
    {
        final FileListData fileData = fileDataList.get(i);

        fileViewHolder.getTextFileName().setText(fileData.getFileName());
        fileViewHolder.getTextFileDate().setText(fileData.getFileDate());

        if (fileData.getFile().isDirectory())
        {
            fileViewHolder.getTextFileSize().setVisibility(View.GONE);
            fileViewHolder.getButtonFileEncrypt().setVisibility(View.GONE);
        }
        else
        {
            fileViewHolder.getTextFileSize().setVisibility(View.VISIBLE);
            String fileSize = Formatter.formatShortFileSize(context, fileData.getFileSize());
            fileViewHolder.getTextFileSize().setText(fileSize);

            fileViewHolder.getButtonFileEncrypt().setVisibility(View.VISIBLE);
        }

        /*if (fileDataList.get(i).getFileIcon() != null)
        {
            fileViewHolder.getImageFileIcon().setScaleType(ImageView.ScaleType.CENTER_CROP);
            fileViewHolder.getImageFileIcon().setImageBitmap(fileDataList.get(i).getFileIcon());
        }*/
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getItemCount()
    {
        return fileDataList.size();
    }

    public FileListData getDataFromPosition(int position)
    {
        return fileDataList.get(position);
    }
}