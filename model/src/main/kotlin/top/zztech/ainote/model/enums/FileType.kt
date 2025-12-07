package top.zztech.ainote.model.enums

import org.babyfish.jimmer.sql.EnumType

@EnumType(EnumType.Strategy.NAME)
enum class FileType {
    /**
     * 图片文件 (jpg, png, gif, svg, webp等)
     */
    IMAGE,
    
    /**
     * 视频文件 (mp4, avi, mov, mkv等)
     */
    VIDEO,
    
    /**
     * 音频文件 (mp3, wav, flac, aac等)
     */
    AUDIO,
    
    /**
     * 文档文件 (pdf, doc, docx, txt, md等)
     */
    DOCUMENT,
    
    /**
     * 电子表格 (xls, xlsx, csv等)
     */
    SPREADSHEET,
    
    /**
     * 演示文稿 (ppt, pptx, key等)
     */
    PRESENTATION,
    
    /**
     * 压缩文件 (zip, rar, 7z, tar, gz等)
     */
    ARCHIVE,
    
    /**
     * 代码/文本 (java, kt, js, html, css, json, xml等)
     */
    CODE,
    
    /**
     * 其他/未知类型
     */
    OTHER
}