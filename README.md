
[PDFBookmarkGener](https://github.com/Megre/PDFBookmarkGener) is a simple tool to generate PDF bookmarks. It extracts titles from **catalog pages** (one or more columns, horizontal layout) of input PDF file. Compared with other tools such as PDFPatcher and AutoBookmark (Adobe Acrobat plugin), [PDFBookmarkGener](https://github.com/Megre/PDFBookmarkGener) works better for PDF files whose content text are extracted using OCR (Optical Character Recognition).

[PDFBookmarkGener](https://github.com/Megre/PDFBookmarkGener) is based on [iText7](https://itextpdf.com/en/products/itext-7).

Usage
----------
The input PDF file should contains text. For PDF file created from images, PDF editors such as Adobe Acrobat can be used to recognize containing text. 

Command line arguments: 

    java -jar PDFBookmarkGener.jar [path] [from]-[to] [offset] [column]

`path`: the path of input PDF file <br>
`from`: the page number where the catalog page starts<br>
`to`: the page number where the catalog page ends<br>
`offset`: number of pages ahead the first chapter<br>
`column (optional, default: 1)`: number of layout columns of the catalog

The output file is in the same path of the input file, whose name is appended with "_PBG".