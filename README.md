# AndroidCodeSamples
Here are some of my solutions. It is intended primarily as answers at StackOverflow.

1. ShareZipped.

It is an answer to
https://stackoverflow.com/questions/53997852/is-it-possible-to-provide-a-zipped-file-in-fileprovider-of-a-file-that-doesnt

ZipableFileProvider is a FileProvider that can zip files on the fly.
It does not create any cached files - it just zips file while another app reads it from FileDescriptor